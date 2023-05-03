# Prerequisites

 - NodeJS (with NPM)
 - wget
 - Maven
 - [JDK 11](https://adoptium.net) 
 - [Lombok](https://projectlombok.org) (Only for IDEs, without it you will not be able to do __anything__)

## Windows
You can either  
a) Use WSL and follow the linux compilation instructions  
b) Follow these directions:
  - You will need a bash terminal, I recommend [git desktop](https://git-scm.com/).
  - [Install NodeJS](https://nodejs.org/en/download/)
  - [Install wget](http://gnuwin32.sourceforge.net/packages/wget.htm)
  - [Download maven, extract it somewhere, add the /bin folder to your path](https://maven.apache.org/download.cgi) (Download the binary zip)
  - Make sure to restart the bash terminal after everything gets added to your path.
  
  

## Linux
(Convert these commands to work for your packagemanager of choice, these work for most Debian based distros (like Ubuntu))
 - `sudo apt install nodejs npm`
 - `sudo apt install wget`
 - `sudo apt install maven`
  
  

## MacOS
[Install homebrew.](https://brew.sh)
 - `brew install nodejs npm`
 - `brew install wget`
 - `brew install maven`
 
 Additionally, you will need to edit the `Info.plist` of your JDK install to include the following lines (put before two `</dict></plist>` tags) (otherwise you will not be able to *locally* run the dev UI, you could still use something like ngrok to proxy it though)
```xml
  <key>NSAppTransportSecurity</key>
  <dict>
    <key>NSAllowsArbitraryLoads</key>
    <true/>
    <key>NSExceptionDomains</key>
    <dict>
      <key>127.0.0.1</key>
      <dict>
        <key>NSExceptionAllowsInsecureHTTPLoads</key>
        <true/>
        <key>NSIncludesSubdomains</key>
        <true/>
      </dict>
    </dict>
  </dict>
```
  
  

# Developing
1) Clone the repo
2) Open the folder in your terminal of choice (Windows users: R-Click the directory > Open Git Bash Here)
3) Run `sh dev.sh`
4) Profit! You can now launch the Bootstrap class with `-D http://localhost:3000` and start developing!

The build files will automatically download the runtime zips and make the executables.

# Building
1) Clone the repo
2) Open the folder in your terminal of choice (Windows users: R-Click the directory > Open Git Bash Here)
3) Run `sh build.sh` (You can optionally pass in "nocompile" or "nodist" to skip either the compilation step or build production step)
4) Profit! See `dist/` for linux, macos, and windows installs.

The build files will automatically download the runtime zips and make the executables.
