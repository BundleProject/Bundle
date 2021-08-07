# Bundle
A program that hooks into minecraft with any mod loader that updates all of your mods with a cool gui.

### bundle.project.json
`bundle.project.json` is recommended for mod developers to implement into their mods.
It is not required for use of Bundle however it is advised.

This file is similar to `fabric.mod.json` or `mcmod.info` and serves mostly
the same purpose. The difference is, it can guarantee that all the version 
and other metadata matches the repository/api.

Here is an example of a `bundle.project.json` for the mod EvergreenHUD
Note that the version **MUST** follow [semver specification](https://semver.org).
```json
{
  "id": "evergreenhud",
  "version": "2.0.0.94-pre.12",
  "minecraft_version": "1.8.9",
  "platform": "forge"
}
```
