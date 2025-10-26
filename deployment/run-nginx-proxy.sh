
podman run --name checkme-proxy \
-v ./nginx-proxy.conf:/etc/nginx/nginx.conf:ro \
-p 8080:8080 \
-d nginx:latest

