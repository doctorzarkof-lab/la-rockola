#!/bin/bash
set -e

echo "== Compilando =="
rm -rf out dist
mkdir -p out dist/lib
javac --release 17 -encoding UTF-8 -cp lib/jlayer-1.0.1.jar -d out estilo/*.java componentes/*.java *.java

echo "== Creando el .jar =="
printf "Main-Class: VentanaPrincipal\nClass-Path: lib/jlayer-1.0.1.jar\n" > manifest.txt
jar cfm dist/LaRockola.jar manifest.txt -C out .
cp lib/jlayer-1.0.1.jar dist/lib/

echo "== Lanzadores =="
cat > dist/LaRockola.sh <<'EOF'
#!/bin/bash
cd "$(dirname "$0")"
java -jar LaRockola.jar
EOF
chmod +x dist/LaRockola.sh

printf '@echo off\r\ncd /d "%%~dp0"\r\nstart "" javaw -jar LaRockola.jar\r\n' > dist/LaRockola.bat

echo "== Comprimiendo =="
cd dist
zip -qr ../LaRockola.zip .
cd ..

echo "Listo: dist/ y LaRockola.zip"
