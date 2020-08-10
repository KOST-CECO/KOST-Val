//For loop
for ( i=0; i<=max-1; i++ )
	i = i + 1;

//For loop with more than one statement
for ( i=0; i<=max-1; i++ )
{
	i = i + 1;
	i = i - 1 ;
}
	
//While loop
i=1;
while ( i<10 && typeof(i)=="number" ) 
{
	i += 1;
}	
	
//Function and if
function lookup (input)
{
  if ( input > 10 )
    return 100;
  else if ( input > 5 && input <= 10 )
    return 200;
  else if ( input > 0 && input <= 5 )
    return 400;
  else
    return 600;
}
result = lookup(7)