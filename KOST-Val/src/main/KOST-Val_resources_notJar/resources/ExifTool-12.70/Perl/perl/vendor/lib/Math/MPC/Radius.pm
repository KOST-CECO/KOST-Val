# Handle the "radius" (mpcr_t) type.
package Math::MPC::Radius;
use strict;
use warnings;

require Exporter;
*import = \&Exporter::import;
require DynaLoader;

our $VERSION = '1.31';
#$VERSION = eval $VERSION;
Math::MPC::Radius->DynaLoader::bootstrap($VERSION);

@Math::MPC::Radius::EXPORT = ();
@Math::MPC::Radius::EXPORT_OK = ();

sub dl_load_flags {0} # Prevent DynaLoader from complaining and croaking
1;
