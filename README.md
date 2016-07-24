# Novucs' FactionsTop
An efficient and comprehensive factions ranking system.

## Supports
* Spigot 1.7.x -> 1.10.x
* FactionsUUID by drtshock
* MassiveCore Factions 2.7+
* H2 and MySQL

## Usage
### Commands
| **Command**  | **Description**            |
| -------------| ---------------------------|
| /ftop <page> | List all top factions      |
| /ftop reload | Reload the plugin settings |

### Signs
Signs can be used to display a faction at a specific rank.

This sign example will display the faction at rank 2:
Line 1: [ftop]
Line 2: 2

### Permissions
| **Permission**         | **Description**                 | **Default** |
| -----------------------| ------------------------------- | ----------- |
| factionstop.use        | List all top factions           | everyone    |
| factionstop.reload     | Reload the plugin settings      | operator    |
| factionstop.sign.break | Break FactionsTop ranking signs | operator    |
| factionstop.sign.place | Place FactionsTop ranking signs | operator    |