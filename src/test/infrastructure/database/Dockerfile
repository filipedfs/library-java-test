# From Postgres.
FROM coldis/infrastructure-transactional-repository

# Copies the server configuration.
COPY conf/*.conf /tmp/
RUN chown -R postgres:postgres /tmp/
