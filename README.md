# Log4E 2026

**Work in progress** - This is a port of the original [Log4E](http://log4e.jayefem.de/) Eclipse plugin by Jayefem to modern Eclipse (2025-12+) and Java 21.

The original Log4E plugin is no longer maintained and incompatible with recent Eclipse versions. This project re-implements it using current Eclipse APIs, Tycho 5.0.1 build system, and SWTBot UI tests.

## Status

- Core infrastructure: done
- 16 command handlers: done
- 8 preference pages: done
- Profile system (SLF4J, Log4j2, JUL): done
- Wizards and dialogs: done
- SWTBot UI tests (248 tests): passing
- **Handler SWTBot tests: in progress**

## Installation

Eclipse update site: https://cristinel-grigoras.github.io/log4e2026/

1. **Help > Install New Software...**
2. **Add...** and enter the URL above
3. Select **Log4E 2026** and follow the wizard

Requirements: Eclipse 2025-12+, Java 21+

## Keyboard Shortcuts

| Action | Shortcut |
|--------|----------|
| Declare Logger | Ctrl+Alt+D |
| Insert Log Statement | Ctrl+Alt+L |
| Log this Variable | Ctrl+Alt+P |
| Log at this Position | Ctrl+Alt+O |
| Log this Method | Ctrl+Alt+I |
| Log this Class | Ctrl+Alt+U |
| Log Errors of Method | Ctrl+Alt+R |
| Log Errors of Class | Ctrl+Alt+E |
| Substitute in Method | Ctrl+Alt+S |
| Substitute in Class | Ctrl+Alt+A |

## Building

```bash
cd workspace/log4e2026
mvn clean verify -DskipTests   # compile only
mvn clean verify               # full build with UI tests (needs xvfb)
```

## Credits

Based on the original Log4E plugin by Jayefem (de.jayefem.log4e).
