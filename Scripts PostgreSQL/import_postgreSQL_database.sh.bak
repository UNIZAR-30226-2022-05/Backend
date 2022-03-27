#!/bin/bash

version="10"
carpetaDB="main"
ficheroDB="$carpetaDB.zip"
directorioPostgreSQL="/var/lib/postgresql/$version"

# Si no está instalado postresql, lo instala
if ! which psql &>/dev/null
then
    echo "PostgreSQL no está instalado. Instalando..."
    sudo apt update
    sudo apt install postgresql -y
    if [ $? != 0 ]
    then
        echo 1>&2 "No se ha podido instalar PostgreSQL"
        exit 1
    fi
    
    echo "PostgreSQL ha sido instalado correctamente"
fi

echo "===== IMPORTADOR DE POSTGRESQL ====="
echo "Advertencia: Esto reemplazará la base de datos de PostgreSQL"
echo "Pulsa ENTER para continuar"
read

if [ ! -f $ficheroDB ]
then
    echo 1>&2 "El fichero $ficheroDB no existe"
    exit 2
fi

# Eliminar el directorio "main" antiguo si lo hay
if [ -d $carpetaDB ]
then
    echo "Eliminando carpeta $carpetaDB antigua..."
    rm -r "$carpetaDB" &>/dev/null
fi

# Descomprimir el fichero main.zip
echo "Descomprimiendo fichero $ficheroDB..."
unzip "$ficheroDB" >/dev/null

# Detener el servicio de PostgreSQL
echo "Deteniendo PostgreSQL..."
sudo service postgresql stop

# Borrar carpeta main de PostgreSQL
sudo rm -r "$directorioPostgreSQL/$carpetaDB"

# Copiar la carpeta main local a PostgreSQL
echo "Importando datos de PostgreSQL..."
sudo cp -r "$carpetaDB" "$directorioPostgreSQL"
sudo chown -R postgres "$directorioPostgreSQL"
rm -r "$carpetaDB"


# Volver a iniciar PostgreSQL
echo "Iniciando PostgreSQL"
sudo service postgresql start

echo -e "\nImportación finalizada"
