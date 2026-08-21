#!/usr/bin/env node

const net = require('node:net');

const listenPort = Number(process.env.DSH_PROXY_PORT || 3080);
const targetPort = Number(process.env.DSH_TARGET_PORT || 3081);
const targetHost = process.env.DSH_TARGET_HOST || '127.0.0.1';

const server = net.createServer((client) => {
  const upstream = net.createConnection({ host: targetHost, port: targetPort });

  client.pipe(upstream);
  upstream.pipe(client);

  const close = () => {
    client.destroy();
    upstream.destroy();
  };

  client.once('error', close);
  upstream.once('error', close);
  client.once('close', () => upstream.destroy());
  upstream.once('close', () => client.destroy());
});

server.listen({ host: '0.0.0.0', port: listenPort }, () => {
  console.log(`DSH TCP forwarder listening on 0.0.0.0:${listenPort} -> ${targetHost}:${targetPort}`);
});

server.on('error', (error) => {
  console.error('DSH TCP forwarder failed:', error);
  process.exit(1);
});
