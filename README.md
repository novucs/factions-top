# Novucs' FactionsTop
An efficient and comprehensive factions ranking system.

This Bukkit plugin adds a command to the well known factions plugin to display
all factions on the server, ordered by wealth. The wealth of a faction is
determined by calculating the value of all blocks and chest contents stored in
the faction claims. This specific implementation aims to provide real-time
results while still maintaining a good performance.

## Supports
* Spigot 1.7+
* FactionsUUID by drtshock
* LegacyFactions by MarkehMe
* FactionsOne
* MassiveCore Factions 2.7+
* H2 and MySQL

## Usage
### Commands
| **Command**      | **Description**                 |
| -----------------| --------------------------------|
| /ftop <page>     | List all top factions in chat   |
| /ftopgui <page>  | List all top factions with GUI  |
| /ftoprecalculate | Recalculates all faction values |
| /ftopreload      | Reload the plugin settings      |
| /ftopversion     | View the plugin version         |

### Permissions
| **Permission**          | **Description**                 | **Default** |
| ------------------------| ------------------------------- | ----------- |
| factionstop.use         | List all top factions           | everyone    |
| factionstop.recalculate | Recalculate all faction values  | operator    |
| factionstop.reload      | Reload the plugin settings      | operator    |
| factionstop.sign.break  | Break FactionsTop ranking signs | operator    |
| factionstop.sign.place  | Place FactionsTop ranking signs | operator    |

## Useful links

- [SpigotMC.org resource page](https://www.spigotmc.org/resources/f-top-factions-top-by-novucs.26905/)
- [Bukkit.org project page](https://dev.bukkit.org/projects/factionstop)
- [Development builds](https://ci.minebench.de/job/FactionsTop/)
- [Issue tracker](https://github.com/novucs/factions-top/issues)

## Development
### Prerequisites
* [Maven](https://maven.apache.org/)
* [OpenJDK 8](https://openjdk.java.net/install/)

### Project Setup (For UNIX)
```sh
# Clone the factions-top repository.
git clone git@github.com:novucs/factions-top.git

# Create and switch to build tools directory, use your own if exists.
mkdir buildtools && cd buildtools

# Fetch latest buildtools version.
wget https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar

# Install required craftbukkit dependencies to maven.
java -jar BuildTools.jar --rev 1.8 && \
  java -jar BuildTools.jar --rev 1.8.3 && \
  java -jar BuildTools.jar --rev 1.8.8

# Change directory to factions-top.
cd ../factions-top

# Install factions-top.
mvn clean install
```
