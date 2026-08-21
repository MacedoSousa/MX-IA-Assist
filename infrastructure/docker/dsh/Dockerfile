FROM node:22-bookworm-slim

ARG DSH_VERSION=0.1.0-rc.8

RUN npm install --global --no-fund --no-audit @deepseek-ai/dsh@${DSH_VERSION} \
    && npm cache clean --force

ENV DSH_HOME=/root/.dsh
ENV NODE_ENV=production

WORKDIR /workspace/mx

COPY tcp-forwarder.cjs /usr/local/bin/tcp-forwarder.cjs

EXPOSE 3080

CMD ["sh", "-c", "dsh --profile web --no-open --host 127.0.0.1 --port 3081 & exec node /usr/local/bin/tcp-forwarder.cjs"]
