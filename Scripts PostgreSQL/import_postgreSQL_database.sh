#!/bin/bash

version="10"
carpetaDB="main"
ficheroDB="$carpetaDB.zip"
directorioPostgreSQL="/var/lib/postgresql/$version"

# Si no está instalado postresql, lo instala
if ! which psql &>/dev/null
then
    echo "PostgreSQL no está instalado. Instalando..."
    
    # Fuente: https://computingforgeeks.com/install-postgresql-12-on-ubuntu/
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
    echo "deb http://apt.postgresql.org/pub/repos/apt/ `lsb_release -cs`-pgdg main" | sudo tee /etc/apt/sources.list.d/pgdg.list
    
    sudo apt update
    sudo apt install "postgresql-$version" -y
    if [ $? != 0 ]
    then
        echo 1>&2 "No se ha podido instalar PostgreSQL"
        exit 1
    fi
    
    echo "PostgreSQL ha sido instalado correctamente"
fi

# Si no está instalado zip, lo instala
if ! which zip &>/dev/null
then
    echo "zip no está instalado. Instalando..."
    sudo apt update
    sudo apt install "zip" -y
    if [ $? != 0 ]
    then
        echo 1>&2 "No se ha podido instalar zip"
        exit 1
    fi
    
    echo "zip ha sido instalado correctamente"
fi

# Si no está instalado unzip, lo instala
if ! which unzip &>/dev/null
then
    echo "unzip no está instalado. Instalando..."
    sudo apt update
    sudo apt install "unzip" -y
    if [ $? != 0 ]
    then
        echo 1>&2 "No se ha podido instalar unzip"
        exit 1
    fi
    
    echo "unzip ha sido instalado correctamente"
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
sudo chown -R postgres:postgres "$directorioPostgreSQL"
sudo chmod "0700" "$directorioPostgreSQL/$carpetaDB"
rm -r "$carpetaDB"


# Volver a iniciar PostgreSQL
echo "Iniciando PostgreSQL"
sudo service postgresql start

echo -e "\nImportación finalizada"
