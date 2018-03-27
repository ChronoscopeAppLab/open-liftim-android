# Color palette converter

This converts (R,G,B) color expression into signed 10 decimal integer.
To use this, compile `colorpaletteconv.go` and execute in a directory that includes
`material_color.json`. Format for raw material_color.json and converted json is below.

Format for material_color_json

```json
[
  {
    "name": "COLOR_NAME",
    "color": "RRR,GGG,BBB"
  },
  {
    "name": "COLOR_NAME_2",
    "color": "RRR,GGG,BBB"
  }
]

```

Format of converted json

```json
[
  {
    "name": "COLOR_NAME",
    "color": -769226
  },
  {
    "name": "COLOR_NAME_2",
    "color": -5138
  }
]
```
