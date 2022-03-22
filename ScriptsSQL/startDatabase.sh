#!/bin/bash
sudo service postgresql start
sudo -i -u postgres
cd '/mnt/c/Users/rauli/Documents/Uno for all/Repositorios/Backend/ScriptsSQL'
psql -U postgres -W -h localhost -p 5432 -d uno_for_all_db