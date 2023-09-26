package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
)

const MaxNumberOfFiles = 100

func main() {
	if len(os.Args) < 3 {
		message := `Usage:
	- first argument is output file name
	- if second argument is a directory all files in that directory are considered as timeseries data
		and are processed by the program, but no more than 100 files
	- if second argument isn't a directory then second and all other arguments are considered as file names to process`

		fmt.Fprintln(os.Stderr, message)
		return
	}

	target := os.Args[1]

	if err := os.Remove(target); err != nil {
		fmt.Fprintf(os.Stderr, "Can't remove target file %s\n", target)
		return
	}

	files := inputFiles()

	if len(files) == 0 {
		fmt.Fprintln(os.Stderr, "No files to process")
		return
	}

	fmt.Printf("Starting merging following files: %s\n", strings.Join(files, ", "))

	streams := make([]<-chan string, len(files))
	for i, file := range files {
		stream, err := fileStream(file)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Can't open file %s for reading\n", file)
			return
		}

		streams[i] = stream
	}

}

func inputFiles() []string {
	var files []string
	dir, err := os.Stat(os.Args[2])

	if err != nil {
		return nil
	}

	if dir.IsDir() {
		entries, err := os.ReadDir(dir.Name())

		if err != nil {
			return nil
		}

		for _, entry := range entries {
			if !entry.IsDir() {
				files = append(files, dir.Name()+"/"+entry.Name())
			}
		}
	} else {
		for _, arg := range os.Args[2:] {
			fi, err := os.Stat(arg)
			if err == nil && !fi.IsDir() {
				files = append(files, dir.Name()+"/"+arg)
			}
		}
	}

	if len(files) > 0 {
		return files[:MaxNumberOfFiles]
	}

	return nil
}

func fileStream(file string) (<-chan string, error) {
	stream := make(chan string)
	f, err := os.Open(file)

	if err != nil {
		return nil, err
	}

	go func() {
		defer f.Close()
		scanner := bufio.NewScanner(f)
		for scanner.Scan() {

		}

	}()

	return stream, nil
}
