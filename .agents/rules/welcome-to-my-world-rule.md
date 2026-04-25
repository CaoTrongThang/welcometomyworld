---
trigger: always_on
---

The User is making a mod for Minecraft Fabric 1.20.1, it adds mobs, dimensions,..., Currently they're adding a new friendly mob that will be owned by the player, it can wear armors and hold weapon, using Geckolib, similar to the Unknown boss but easier, see what are they up to and help them!

Remember these rules:

- You are running on: Windows 11.
- Shell: Use PowerShell ONLY (NOT cmd, NOT bash).
- Java may NOT be in the PATH.
- Limit your searching. DO NOT try to read the source code of other mods or search endlessly (Get-ChildItem). STOP and ask - the user to show you the class.
- Always prioritize PowerShell-native commands.
- Avoid tools that are not exist on a default system (e.g., jar, unzip, bash, grep, javap). Prefer Expand-Archive for .zip/.jar files.
- Use correct, unescaped Windows paths (e.g., C:\path\to\file, do not use \\ or \").
- Never assume environment variables or tools exist unless explicitly specified.
- Use explicit .exe extensions for native Windows commands (e.g., use curl.exe instead of curl to avoid PowerShell alias conflicts).
- Do not waste time complimenting the user. Focus on improving their code and eliminating tech debt.
- When fixing code, do not just keep adding new lines. Remove or replace the old, non-working code first.
- If the user has a bad idea that is unfit for production, correct them immediately.
- Tech debt is dangerous. Keep all implementations simple and efficient. Do not overcomplicate your solutions.
- Before modifying anything, carefully check what currently uses it and understand its exact purpose.
- The user often uses poor naming conventions and bad code structure. Proactively suggest renaming variables/functions and correcting - the architecture.
- For debug logging, always pass the short variable name as the first parameter (Example: LOGGER.info("param" + param)).