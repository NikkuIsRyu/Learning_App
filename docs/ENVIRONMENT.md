# Environment & Toolchain Status

Generated as part of ticket `T0.1` (Toolchain probe).

| Tool | Version | Path / Location | Status | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **android** | MISSING | Not found on PATH | **MISSING** | Required for Android project creation, previews, and device runs. Documented installer: `curl -fsSL https://dl.google.com/android/cli/latest/windows_x86_64/install.cmd -o "%TEMP%\i.cmd" && "%TEMP%\i.cmd"` |
| **java** | 1.8.0_333 (system) / 17.0.7 (Android Studio JBR) | System: `C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe`<br/>JBR: `D:\Program Files (x86)\Android\Android Studio\jbr\bin\java.exe` | **NEEDS_CONFIG** | System PATH points to Java 8. JDK 17.0.7 is available in Android Studio JBR at `D:\Program Files (x86)\Android\Android Studio\jbr`. Set `JAVA_HOME` or `org.gradle.java.home` in `gradle.properties`. |
| **git** | 2.47.0.windows.1 | `C:\Program Files\Git\cmd\git.exe` | **OK** | Ready |
| **node** | v24.19.0 | `C:\Program Files\nodejs\node.exe` | **OK** | Node 22+ requirement satisfied |
| **uv** | MISSING | Not found on PATH | **MISSING** | Required for Python ETL tasks (`mdx_vocab.py`, `kanji_build.py`). Documented installer: `irm https://astral.sh/uv/install.ps1 \| iex` |

## Environment Details
- **OS**: Windows 11 / Windows_NT (x86_64)
- **Workspace Path**: `d:\.D Dokuments\Uni Stuff\Knowledge_App`
- **Android SDK Path**: `C:\Users\nickb\AppData\Local\Android\Sdk` (installed with build-tools 30.0.3, 33.0.2, 34.0.0, 36.1.0)
- **Android Studio Path**: `D:\Program Files (x86)\Android\Android Studio` (Version 2023.1)
