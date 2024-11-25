# Resource Management Game

A simple resource management game implemented in Java.

## Project Structure

```
ResourceGame/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── resourcegame/
│       │           ├── core/
│       │           ├── entities/
│       │           ├── systems/
│       │           ├── ui/
│       │           └── utils/
│       └── resources/
└── build/
```

## Building and Running

1. Make sure you have Java 11 or later installed
2. Run the build script:
   ```bash
   ./build.sh
   ```

## Development

The project is organized into several packages:
- `core`: Contains the main game logic
- `entities`: Contains game entities like Player, Resources, etc.
- `systems`: Contains game systems like Crafting and Market
- `ui`: Contains the user interface components
- `utils`: Contains utility classes and enums
