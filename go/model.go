package model

import (
	"fmt"
	"log"
	"strconv"
	"strings"
	"time"
)

const DateFormat = "2006-01-02"

type Record struct {
	Date  time.Time
	Value int
}

func ParseRecord(s string) *Record {
	splitted := strings.Split(s, ":")

	if len(splitted) != 2 {
		return nil
	}

	date, errDate := time.Parse(DateFormat, splitted[0])
	value, errValue := strconv.Atoi(splitted[1])

	if errDate != nil || errValue != nil {
		return nil
	}

	return &Record{date, value}
}

func (r *Record) Add(other Record) {
	if !r.Date.Equal(other.Date) {
		log.Fatalln("Dates should be equal")
	}
	r.Value += other.Value
}

func (r *Record) String() string {
	return fmt.Sprintf("%s:%d", r.Date.Format(DateFormat), r.Value)
}
