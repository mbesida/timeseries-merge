package main

import (
	"bufio"
	"fmt"
	"os"
	"strings"
	"time"
	model "timeseries-merge"

	"github.com/igrmk/treemap/v2"
)

const MaxNumberOfFiles = 100

type item struct {
	value   int
	streams []<-chan string
}

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

	if err := os.Remove(target); err != nil && !os.IsNotExist(err) {
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

	if e := mergeStreams(streams, target); e != nil {
		fmt.Fprintf(os.Stderr, "Can't write to target file %s\n", target)
		return
	}
}

func inputFiles() []string {
	var files []string
	potentialDirName := os.Args[2]
	dir, err := os.Stat(potentialDirName)

	if err != nil {
		return nil
	}

	if dir.IsDir() {
		entries, err := os.ReadDir(potentialDirName)

		if err != nil {
			fmt.Fprintf(os.Stderr, "Can't read dir %s\n", potentialDirName)
			return nil
		}

		for _, entry := range entries {
			if !entry.IsDir() {
				files = append(files, potentialDirName+"/"+entry.Name())
			}
		}
	} else {
		for _, arg := range os.Args[2:] {
			fi, err := os.Stat(arg)
			if err == nil && !fi.IsDir() {
				fmt.Println(fi.Name())
				files = append(files, arg)
			}
		}
	}

	if len(files) > 0 {
		return files[:min(MaxNumberOfFiles, len(files))]
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
		defer close(stream)
		scanner := bufio.NewScanner(f)
		for scanner.Scan() {
			line := scanner.Text()
			stream <- line
		}
	}()

	return stream, nil
}

func mergeStreams(streams []<-chan string, targetFile string) error {
	f, err := os.Create(targetFile)

	if err != nil {
		return err
	}

	w := bufio.NewWriter(f)

	defer func() {
		w.Flush()
		f.Close()
	}()

	grouped := treemap.NewWithKeyCompare[time.Time, item](func(a, b time.Time) bool {
		return a.Before(b)
	})

	for _, stream := range streams {
		readLineFromStream(stream, grouped)
	}

	for grouped.Len() != 0 {
		date, item := grouped.Iterator().Key(), grouped.Iterator().Value()
		record := model.Record{Date: date, Value: item.value}
		w.WriteString(record.String() + "\n")
		grouped.Del(date)
		for _, s := range item.streams {
			readLineFromStream(s, grouped)
		}
	}

	return nil
}

func readLineFromStream(stream <-chan string, data *treemap.TreeMap[time.Time, item]) {
	line, ok := <-stream
	if ok {
		r := model.ParseRecord(line)
		if r != nil {
			v, exists := data.Get(r.Date)
			if exists {
				data.Set(r.Date, item{v.value + r.Value, append(v.streams, stream)})
			} else {
				data.Set(r.Date, item{r.Value, []<-chan string{stream}})
			}
		}
	}
}
