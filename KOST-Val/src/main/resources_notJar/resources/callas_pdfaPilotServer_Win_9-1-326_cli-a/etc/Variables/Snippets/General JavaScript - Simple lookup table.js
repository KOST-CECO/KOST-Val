//Very simple lookup table

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
