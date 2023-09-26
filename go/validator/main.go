package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strconv"
	"strings"
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
		splitted := strings.Split(line, ":")

		if len(splitted) != 2 {
			log.Fatalf("Incorrect format of a file. Line is %s, err msg is %s\n", line, err.Error())
		}

		dateStr := splitted[0]
		valueStr := splitted[1]

		date, errDate := time.Parse(model.DateFormat, dateStr)
		_, errValue := strconv.Atoi(valueStr)

		if errDate != nil || errValue != nil {
			log.Fatalln("Incorrect format of a line in file")
		}

		if previousDate == nil {
			result = true
			previousDate = &date
		} else {
			result = previousDate.Before(date)
			if !result {
				break
			}
			*previousDate = date
		}
	}

	if result {
		fmt.Println("File is valid")
	} else {
		fmt.Println("File is invalid")
	}
}
