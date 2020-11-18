const path = require('path');
const express = require('express');
const bodyParser = require('body-parser');
const cors = require('cors');
const fetch = require('node-fetch');
const dotenv = require('dotenv');
const server = express();

const {parsed: env} = dotenv.config({
  path: path.join(__dirname, '../.env')
})

server.use(bodyParser.json());
server.use(cors());

server.use('/token', proxy({
  to: env.PRIMER_MERCHANT_API_URL + '/auth/client-token',
  format: replaceUrls,
  headers: {
    'X-Api-Key': env.PRIMER_API_KEY
  }
}))

server.use('/pci', proxy({
  to: env.PRIMER_PCI_API_URL,
}));

server.use(
  "/api",
  proxy({
    to: env.PRIMER_MERCHANT_API_URL,
    format: replaceUrls,
  })
);

server.listen(80, () => {
  console.log('Proxy server listeninng');
});

function proxy (options) {
  return async (req, res) => {
    const { format = identity, headers = {} } = options;
    
    const url = options.to + req.url.replace(/\/$/, '');

    const init = { 
      method: req.method,
      headers: { ...req.headers, ...headers },
    };

    if (req.method.toLowerCase() === 'post') {
      init.body = JSON.stringify(req.body);
    }

    console.log('Making Request: ' + url, init.headers);

    const response = await fetch(url, init);
    const json = await response.json();

    const data = format(json);

    console.log('Response from Primer:');
    console.log(response.status, data);

    return res.status(response.status).send(data);
  };
}

function identity(v) {
  return v;
}

function replaceUrls(input) {
  let result = { ...input };

  if (input.clientToken) {
    result = { ...result, clientToken: replaceClientToken(input.clientToken) };
  }

  if (input.coreUrl) {
    result = { ...result, coreUrl: `${env.ANDROID_LOCALHOST_PROXY}/api` };
  }

  if (input.pciUrl) {
    result = { ...result, pciUrl: `${env.ANDROID_LOCALHOST_PROXY}/pci` };
  }

  return result;
}

function replaceClientToken(token) {
  const parts = token.split('.');
  const decoded = Buffer.from(parts[1], "base64").toString("utf-8");
  const claims = JSON.parse(decoded);

  claims.configurationUrl = `${env.ANDROID_LOCALHOST_PROXY}/api/client-sdk/configuration`;

  const encoded = Buffer.from(JSON.stringify(claims), 'utf-8').toString('base64');

  return [parts[0], encoded, parts[2]].join('.');
}