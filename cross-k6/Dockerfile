FROM grafana/k6

# Must be root to add CROSS Custom CA to system's trusted pool
USER root
ADD CA.crt /usr/local/share/ca-certificates/cross.crt
RUN chmod 644 /usr/local/share/ca-certificates/cross.crt && update-ca-certificates

# Revert back to unprivileged user used by k6 https://github.com/grafana/k6/blob/master/Dockerfile
USER 12345

# Copy the bundled test scripts
COPY ./dist /dist