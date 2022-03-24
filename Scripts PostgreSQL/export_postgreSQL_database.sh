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

echo "===== EXPORTADOR DE POSTGRESQL ====="
echo "Advertencia: Esto reemplazará los archivos $carpetaDB y $ficheroDB de la carpeta actual"
echo "Pulsa ENTER para continuar"
read

# Eliminar el fichero "main.zip" antiguo si lo hay
if [ -f $ficheroDB ]
then
    echo "Eliminando fichero $ficheroDB antiguo..."
    rm "$ficheroDB" &>/dev/null
fi

# Eliminar el directorio "main" antiguo si lo hay
if [ -d $carpetaDB ]
then
    echo "Eliminando carpeta $carpetaDB antigua..."
    rm -r "$carpetaDB" &>/dev/null
fi

# Detener el servicio de PostgreSQL
echo "Deteniendo PostgreSQL..."
sudo service postgresql stop

# Copiar el contenido de la base de datos al directorio actual
echo "Exportando datos de PostgreSQL..."
sudo cp -p -r "$directorioPostgreSQL/$carpetaDB" "."

# Se hace dueño de dicha carpeta al usuario actual
usuario="$USER"
sudo chown -R "$USER:$USER" "$carpetaDB"

# Comprimir la carpeta "main"
echo "Comprimiendo carpeta main de PostgreSQL..."
(zip -r "$ficheroDB" "$carpetaDB" >/dev/null) && rm -r "$carpetaDB"


# Volver a iniciar PostgreSQL
echo "Iniciando PostgreSQL"
sudo service postgresql start

echo -e "\nExportación finalizada"
