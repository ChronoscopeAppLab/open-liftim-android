/*
 * Copyright 2018 Chronoscope
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package main

import (
    "os"
    "log"
    "bufio"
    "io"
    "encoding/json"
    "fmt"
    "strconv"
)

type Color struct {
    Name  string `json:"name"`
    Color string `json:"color"`
}

type NewColor struct {
    Name  string `json:"name"`
    Color int32  `json:"color"`
}

func main() {
    file, err := os.Open("./material_color.json")
    if err != nil {
        log.Fatal(err)
    }
    defer file.Close()
    reader := bufio.NewReader(file)
    bytes := make([]byte, 0)
    for {
        read, err := reader.ReadByte()
        if err == io.EOF {
            break
        } else if err != nil {
            log.Fatal(err)
        } else {
            bytes = append(bytes, read)
        }
    }
    var colors []Color
    err = json.Unmarshal(bytes, &colors)
    if err != nil {
        log.Fatal(err)
    }
    newColors := make([]NewColor, 0)
    for _, color := range colors {
        name := color.Name
        alpha := 255
        r, g, b := getRGB(color.Color)
        converted := (alpha << 24) | (r << 16) | (g << 8) | b
        newColors = append(newColors, NewColor{name, int32(converted)})
    }
    data, err := json.Marshal(newColors)
    if err != nil {
        log.Fatal(err)
    }
    fmt.Println(string(data))
}

func getRGB(color string) (int, int, int) {
    colorsAsStr := make([]string, 3)
    spos := 0
    for _, char := range color {
        if char == ',' {
            spos++
        } else {
            colorsAsStr[spos] += string(char)
        }
    }
    r, _ := strconv.ParseInt(colorsAsStr[0], 10, 32)
    g, _ := strconv.ParseInt(colorsAsStr[1], 10, 32)
    b, _ := strconv.ParseInt(colorsAsStr[2], 10, 32)
    return int(r), int(g), int(b)
}
