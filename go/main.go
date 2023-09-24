package main

import (
	"bufio"
	"fmt"
	"log"
	"math/rand"
	"os"
	"path/filepath"
	"time"
)

const OutputFolder = "../data"

func main() {
	dir, err := os.Stat(OutputFolder)

	absoluteDirPath, _ := filepath.Abs(OutputFolder)
	if err == nil {
		if dir.IsDir() {
			fmt.Println("Removing directory", absoluteDirPath)
			if e := os.RemoveAll(OutputFolder); e != nil {
				log.Fatalln("Can't remove folder " + absoluteDirPath)
			}
		} else {
			// remove file with name "data"
			os.Remove(OutputFolder)
		}
	}

	fmt.Println("Creating directory", absoluteDirPath)
	os.Mkdir(OutputFolder, os.ModePerm)

	for i := 1; i <= 100; i++ {
		date := time.Date(1900, time.January, 1, 0, 0, 0, 0, time.UTC)
		date = date.AddDate(i, 0, 0)

		name := fmt.Sprintf("%s/file%d.dat", OutputFolder, i)
		f, err := os.OpenFile(name, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0644)

		if err != nil {
			log.Fatalf("Can't create data file %s", err)
		}

		out := bufio.NewWriterSize(f, 1024*8)

		for j := 1; j <= 500000; j++ {
			record := Record{date.AddDate(0, 0, j), rand.Intn(100) + 1}
			out.WriteString(record.String() + "\n")
		}

		out.Flush()
		f.Close()
	}

}
