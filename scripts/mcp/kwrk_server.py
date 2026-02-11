import subprocess
from mcp.server.fastmcp import FastMCP

# Initialize the MCP server
mcp = FastMCP("Kwrk Load Tester")

@mcp.tool()
def run_load_test(url: str, rate: float = 10.0) -> str:
    """
    Runs a load test against a REST API using kwrk.
    
    Args:
        url: The full URL of the REST API endpoint to test.
        rate: The requests per second (RPS) to sustain.
    """
    command = [
        "jbang", 
        "kwrk@wfouche", 
        "--url", url, 
        "--rate", str(rate)
    ]
    
    try:
        # Execute the command and capture output
        result = subprocess.run(
            command, 
            capture_output=True, 
            text=True, 
            check=True
        )
        return f"Load Test Results for {url}:\n\n{result.stdout}"
    
    except subprocess.CalledProcessError as e:
        return f"Error executing kwrk: {e.stderr}"
    except Exception as e:
        return f"An unexpected error occurred: {str(e)}"

if __name__ == "__main__":
    mcp.run()