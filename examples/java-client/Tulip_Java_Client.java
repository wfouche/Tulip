import java.lang.*;

public class Tulip_Java_Client
{
	/* ---------------------------------------------------------- */
	
	private int user_id;

	/* ---------------------------------------------------------- */

	//
	// Each user object (Tulip_Java__Client) is single-threaded.
	//
	public Tulip_Java_Client(int _user_id)
	{
		// Init code goes here.
		this.user_id = _user_id;
		System.out.println("      (Java) User ID = " + this.user_id);
	}
	
	/* ---------------------------------------------------------- */

	public boolean done()
	{
		// Cleanup and close the object.
		// ...
		return true;
	}

	/* ---------------------------------------------------------- */

	public Boolean test1()
	{
		try
		{
			// Implement the benchmark operation here.
			Thread.sleep(10);
		}
		catch (Exception e)
        {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
        }
		return true;
	}

	/* ---------------------------------------------------------- */

	public Boolean test2()
	{
		try
		{
			// Implement the benchmark operation here.
			Thread.sleep(20);
		}
		catch (Exception e)
        {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
        }
		return true;
	}

	/* ---------------------------------------------------------- */

	public Boolean test3()
	{
		try
		{
			// Implement the benchmark operation here.
			Thread.sleep(30);
		}
		catch (Exception e)
        {
            System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
        }
		return true;
	}

	/* ---------------------------------------------------------- */

	public static void main(String[] args)
	{
		Tulip_Java_Client a = new Tulip_Java_Client(0);

		System.out.println("Tulip_Java_Client: test routines.");

		for (;;)
		{
			a.test1();
			a.test2();
			a.test3();
		}
	}

	/* ---------------------------------------------------------- */
}
