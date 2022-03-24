#!/bin/bash

version="10"

echo "===== DESINSTALADOR DE POSTGRESQL ====="
echo "Advertencia: esto borrará todos los datos de PostgreSQL de manera irreversible"
echo -n "Escribe YES para confirmar el borrado: "
read respuesta
if [ $respuesta ] && [ $respuesta == "YES" ]
then
    echo "Borrando datos de PostgreSQL %version%"
    sudo apt --purge remove postgresql -y
    sudo apt-get --purge remove postgresql postgresql-$version postgresql-client-$version postgresql-client-common postgresql-common -y 
    
    sudo rm -rf /var/lib/postgresql/
    sudo rm -rf /var/log/postgresql/
    sudo rm -rf /etc/postgresql/
    
    sudo deluser postgres
    
    echo "Desinstalación completada"
else
    echo "Desinstalación cancelada"
fi
