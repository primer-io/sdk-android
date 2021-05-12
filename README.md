[![Maven Central](https://img.shields.io/maven-central/v/io.primer/android.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.primer%22%20AND%20a:%22android%22)

# primer-sdk-android

## Tooling
- android-studio
- nodejs >= 12 + yarn (for local server)

## Running Virtual device with your local server

Android virtual devices proxy `10.0.2.2` to your machine's localhost. There's a proxy script in the `/proxy` to help adapt the URLs returned from the server to account for this:

```bash
# Copy the .env.example
cp .env.example .env

# Add your API key
nano .env

# set up and run the proxy
cd proxy
yarn install
node server.js

# All the requests from the virtual device should now go to your running server
```

## Running

To run the example, simply press the play buttonn from android studio to launch on a virtual device.

Logcat has a habbit of misbehaving so you mmight need to attach the debugger and set breakpoints to find out what's really going on.
