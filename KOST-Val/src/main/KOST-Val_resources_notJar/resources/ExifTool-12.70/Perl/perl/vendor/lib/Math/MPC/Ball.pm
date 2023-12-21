# Handle the "ball" (mpcb_t) type.
package Math::MPC::Ball;
use strict;
use warnings;

require Exporter;
*import = \&Exporter::import;
require DynaLoader;

our $VERSION = '1.31';
#$VERSION = eval $VERSION;
Math::MPC::Ball->DynaLoader::bootstrap($VERSION);

@Math::MPC::Ball::EXPORT = ();
@Math::MPC::Ball::EXPORT_OK = ();

sub dl_load_flags {0} # Prevent DynaLoader from complaining and croaking
1;
