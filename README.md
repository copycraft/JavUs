# JavUs

**JavUs** is a small experimental **single-player desktop game** written in **Java 21** using **LWJGL (OpenGL + GLFW)**.  
It is loosely inspired by *Among Us*, built mainly as a learning / fun project.

The game is **semi-playable**, has **no in-game text explanations**, and is currently **unfinished** because I lost inspiration for further development.

---

## Features

- Desktop game (Windows / Linux / macOS)
- Java 21
- LWJGL (OpenGL rendering, GLFW input)
- Procedurally generated map with rooms and corridors
- Player + simple AI bots
- Kill & report system
- Voting screen
- Minimap
- Mouse + keyboard controls

---

## Project Status

⚠️ **Unfinished / Abandoned Prototype**

- Core mechanics exist
- No win/lose conditions
- No sound
- Minimal UI
- Voting has no logic beyond selection
- Bots are very simple
- Project stopped due to loss of inspiration

Still useful as:
- A learning reference
- An LWJGL example
- A base for future experiments

---

## Requirements

- **Java 21**
- LWJGL properly set up (OpenGL, GLFW, STB)
- Desktop GPU with OpenGL support

---

## How to Run

1. Make sure Java 21 is installed
2. Open the project in your IDE
3. Run the main class: `org.example.Main`

A window titled **JavUs** will open.

---

## How to Play

### Main Menu
- When the game launches, you’ll see a **big blue button**
- **Click the blue button** (or press **Enter**) to start single-player mode

### Movement
- **W A S D** — move your character around the map

### Buttons (Top-Right)

- 🔴 **Red Button — Kill**
  - Kills the closest bot if you are near one
  - Has a cooldown

- 🟡 **Yellow Button — Report**
  - Used to report a dead body
  - Reporting actually happens automatically if you are close enough

### Meetings & Voting
- When a body is reported, a **voting screen** appears
- You must **click one of the players or bots**
- There is **no logic** behind voting yet  
  👉 just **pick a random player** to eject
- After voting, the game continues

---

## Controls Summary

| Action | Control |
|------|--------|
| Move | W / A / S / D |
| Kill | Click red button |
| Report | Click yellow button |
| Vote | Click a player on voting screen |
| Start Game | Click blue button / Enter |

---

## Code Structure (Overview)

- `Main` — window setup, game loop, state switching
- `Menu` — main menu rendering & input
- `Game` — core gameplay logic
- `MapGen` — procedural map generation
- `Bot` — simple AI entities
- `DeadBody` — dead body representation
- `VotingScreen` — voting UI and logic

---

## License

No license specified.  
Use, modify, or learn from it freely.
