package main

import (
	"fmt"
	"net/http"
	"os"
    "io"
	"github.com/gin-gonic/gin"
)

func main() {
	// Set Gin to release mode to reduce console noise, 
	// similar to configuring slf4j-simple
	gin.SetMode(gin.ReleaseMode)
	gin.DefaultWriter = io.Discard

	r := gin.Default()

	// Reusable handler for the "OK" JSON response
	okResponse := func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"code": "OK"})
	}

	// Routes
	r.GET("/", okResponse)

	r.GET("/exit", func(c *gin.Context) {
		fmt.Println("Exit.")
		os.Exit(0)
	})

	// Posts
	r.POST("/posts/", okResponse)
	r.GET("/posts/:id", okResponse)
	r.PUT("/posts/:id", okResponse)
	r.DELETE("/posts/:id", okResponse)

	// Other Resources
	r.GET("/comments/:id", okResponse)
	r.GET("/albums/:id", okResponse)
	r.GET("/photos/:id", okResponse)
	r.GET("/todos/:id", okResponse)

	// Rendezvous
	r.GET("/rendezvous", func(c *gin.Context) {
		body, _ := c.GetRawData()
		fmt.Println(string(body))
		c.JSON(http.StatusOK, gin.H{"code": "OK"})
	})

	fmt.Println("Starting server on port 7070...")
	r.Run(":7070")
}
