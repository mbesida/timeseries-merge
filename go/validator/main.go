package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"time"
	model "timeseries-merge"
)

func main() {
	if len(os.Args) < 2 {
		fmt.Fprintln(os.Stderr, "Usage: validator <filename>")
		return
	}

	f, err := os.Open(os.Args[1])

	if err != nil {
		log.Fatalf("Can't open file %s. Reason %s", os.Args[1], err.Error())
	}

	defer f.Close()

	scanner := bufio.NewScanner(f)

	var previousDate *time.Time

	result := false

	for scanner.Scan() {
		line := scanner.Text()
		r := model.ParseRecord(line)
		if r == nil {
			log.Fatalf("Incorrect data format. Line is %s\n", line)
		}

		if previousDate == nil {
			result = true
			previousDate = &r.Date
		} else {
			result = previousDate.Before(r.Date)
			if !result {
				break
			}
			*previousDate = r.Date
		}
	}

	if result {
		fmt.Println("File is valid")
	} else {
		fmt.Println("File is invalid")
	}
}
