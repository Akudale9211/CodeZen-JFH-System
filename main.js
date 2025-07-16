const { app, BrowserWindow } = require("electron");
const path = require("path");
const waitPort = require("wait-port");
const { spawn, exec } = require("child_process");

let springApp;
let mainWindow;

// Create the Electron browser window
function createMainWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    webPreferences: {
      contextIsolation: true,
    },
  });

  mainWindow.loadURL("http://localhost:8080");

  mainWindow.on("closed", async () => {
    await shutdownApp();
  });
}

// Start the Spring Boot JAR process
function startSpringBoot() {
  const jarPath = path.join(__dirname, "marketyardbill-0.0.1-SNAPSHOT.jar");

  springApp = spawn("java", ["-jar", jarPath], {
    windowsHide: true,
    detached: false,
    stdio: ["ignore", "pipe", "pipe"],
  });

  springApp.stdout.on("data", (data) =>
    console.log(`[Spring Boot] ${data.toString().trim()}`)
  );

  springApp.stderr.on("data", (data) =>
    console.error(`[Spring Boot ERROR] ${data.toString().trim()}`)
  );

  springApp.on("exit", (code) => {
    console.log(`Spring Boot exited with code ${code}`);
  });
}

// Kill any process using a specific port
function killProcessOnPort(port) {
  return new Promise((resolve) => {
    const isWin = process.platform === "win32";

    const command = isWin
      ? `netstat -ano | findstr :${port}`
      : `lsof -ti tcp:${port}`;

    exec(command, (err, stdout) => {
      if (err || !stdout) return resolve();

      const pids = isWin
        ? stdout
            .trim()
            .split("\n")
            .map((line) => line.trim().split(/\s+/).pop())
        : stdout.trim().split("\n");

      if (pids.length === 0) return resolve();

      let completed = 0;
      pids.forEach((pid) => {
        if (!pid) return checkComplete();

        const killCmd = isWin
          ? `taskkill /PID ${pid} /F /T`
          : `kill -9 ${pid}`;

        exec(killCmd, (killErr) => {
          if (!killErr) {
            console.log(`✅ Killed PID ${pid} on port ${port}`);
          }
          checkComplete();
        });
      });

      function checkComplete() {
        completed++;
        if (completed === pids.length) resolve();
      }
    });
  });
}

// Kill processes that may conflict
async function releaseOccupiedPorts() {
  await killProcessOnPort(8080);
  await killProcessOnPort(3000);
}

// Full shutdown sequence
async function shutdownApp() {
  await releaseOccupiedPorts();
  if (springApp) springApp.kill();
  app.quit();
}

// Entry point when app is ready
app.whenReady().then(async () => {
  await releaseOccupiedPorts();
  startSpringBoot();

  const portOpen = await waitPort({
    host: "localhost",
    port: 8080,
    timeout: 30000,
  });

  if (portOpen) {
    console.log("✅ Spring Boot is up. Launching Electron window...");
    createMainWindow();
  } else {
    console.error("❌ Spring Boot did not respond on port 8080. Exiting...");
    await shutdownApp();
  }
});

// Close the app on all windows closed
app.on("window-all-closed", async () => {
  await shutdownApp();
});
