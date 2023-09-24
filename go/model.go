package main

import (
	"fmt"
	"log"
	"time"
)

const dateFormat = "2006-01-02"

type Record struct {
	Date  time.Time
	Value int
}

func NewRecord(s string) *Record {
	var dateStr string
	var value int

	parsed, _ := fmt.Sscanf(s, "%s:%d", &dateStr, &value)

	if parsed != 2 {
		return nil
	}

	date, err := time.Parse(dateFormat, dateStr)

	if err != nil {
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
	return fmt.Sprintf("%s:%d", r.Date.Format(dateFormat), r.Value)
}
