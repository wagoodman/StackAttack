package com.wagoodman.stackattack;

public class UnderflowException extends RuntimeException
{

	private static final long serialVersionUID = 1496214153972227302L;

	public UnderflowException(String message)
	{
		super(message);
	}
}