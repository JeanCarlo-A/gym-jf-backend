# MySQL con utf8mb4 forzado también en el cliente que ejecuta los scripts de inicialización.
# La configuración va dentro de la imagen (no montada) porque MySQL ignora los .cnf con
# permisos de escritura para todos, que es como Windows monta los archivos en Docker.
FROM mysql:8.4
COPY charset.cnf /etc/mysql/conf.d/charset.cnf
RUN chmod 644 /etc/mysql/conf.d/charset.cnf
