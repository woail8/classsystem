import fs from 'node:fs'
import path from 'node:path'

const projectRoot = process.cwd()
const distDir = path.join(projectRoot, 'dist')
const staticDir = path.join(projectRoot, 'server', 'src', 'main', 'resources', 'static')

function removeDirectory(targetPath) {
  if (fs.existsSync(targetPath)) {
    fs.rmSync(targetPath, { recursive: true, force: true })
  }
}

function copyDirectory(sourcePath, targetPath) {
  fs.mkdirSync(targetPath, { recursive: true })
  for (const entry of fs.readdirSync(sourcePath, { withFileTypes: true })) {
    const sourceEntry = path.join(sourcePath, entry.name)
    const targetEntry = path.join(targetPath, entry.name)

    if (entry.isDirectory()) {
      copyDirectory(sourceEntry, targetEntry)
    } else {
      fs.copyFileSync(sourceEntry, targetEntry)
    }
  }
}

if (!fs.existsSync(distDir)) {
  throw new Error('前端构建产物 dist 不存在，请先执行 npm run build')
}

removeDirectory(staticDir)
copyDirectory(distDir, staticDir)

console.log(`已同步前端静态资源到 ${staticDir}`)
