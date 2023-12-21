#####################################################################
#
# the Perl::Tidy::FileWriter class writes the output file
#
#####################################################################

package Perl::Tidy::FileWriter;
use strict;
use warnings;
our $VERSION = '20230701';

use constant DEVEL_MODE   => 0;
use constant EMPTY_STRING => q{};

sub AUTOLOAD {

    # Catch any undefined sub calls so that we are sure to get
    # some diagnostic information.  This sub should never be called
    # except for a programming error.
    our $AUTOLOAD;
    return if ( $AUTOLOAD =~ /\bDESTROY$/ );
    my ( $pkg, $fname, $lno ) = caller();
    my $my_package = __PACKAGE__;
    print STDERR <<EOM;
======================================================================
Error detected in package '$my_package', version $VERSION
Received unexpected AUTOLOAD call for sub '$AUTOLOAD'
Called from package: '$pkg'  
Called from File '$fname'  at line '$lno'
This error is probably due to a recent programming change
======================================================================
EOM
    exit 1;
} ## end sub AUTOLOAD

sub DESTROY {

    # required to avoid call to AUTOLOAD in some versions of perl
}

my $input_stream_name = EMPTY_STRING;

# Maximum number of little messages; probably need not be changed.
use constant MAX_NAG_MESSAGES => 6;

BEGIN {

    # Array index names for variables.
    # Do not combine with other BEGIN blocks (c101).
    my $i = 0;
    use constant {
        _line_sink_object_            => $i++,
        _logger_object_               => $i++,
        _rOpts_                       => $i++,
        _output_line_number_          => $i++,
        _consecutive_blank_lines_     => $i++,
        _consecutive_nonblank_lines_  => $i++,
        _consecutive_new_blank_lines_ => $i++,
        _first_line_length_error_     => $i++,
        _max_line_length_error_       => $i++,
        _last_line_length_error_      => $i++,
        _first_line_length_error_at_  => $i++,
        _max_line_length_error_at_    => $i++,
        _last_line_length_error_at_   => $i++,
        _line_length_error_count_     => $i++,
        _max_output_line_length_      => $i++,
        _max_output_line_length_at_   => $i++,
        _rK_checklist_                => $i++,
        _K_arrival_order_matches_     => $i++,
        _K_sequence_error_msg_        => $i++,
        _K_last_arrival_              => $i++,
        _save_logfile_                => $i++,
    };
} ## end BEGIN

sub Die {
    my ($msg) = @_;
    Perl::Tidy::Die($msg);
    return;
}

sub Fault {
    my ($msg) = @_;

    # This routine is called for errors that really should not occur
    # except if there has been a bug introduced by a recent program change.
    # Please add comments at calls to Fault to explain why the call
    # should not occur, and where to look to fix it.
    my ( $package0, $filename0, $line0, $subroutine0 ) = caller(0);
    my ( $package1, $filename1, $line1, $subroutine1 ) = caller(1);
    my ( $package2, $filename2, $line2, $subroutine2 ) = caller(2);
    my $pkg = __PACKAGE__;

    Die(<<EOM);
==============================================================================
While operating on input stream with name: '$input_stream_name'
A fault was detected at line $line0 of sub '$subroutine1'
in file '$filename1'
which was called from line $line1 of sub '$subroutine2'
Message: '$msg'
This is probably an error introduced by a recent programming change.
$pkg reports VERSION='$VERSION'.
==============================================================================
EOM

    # This return is to keep Perl-Critic from complaining.
    return;
} ## end sub Fault

sub warning {
    my ( $self, $msg ) = @_;
    my $logger_object = $self->[_logger_object_];
    if ($logger_object) { $logger_object->warning($msg); }
    return;
} ## end sub warning

sub write_logfile_entry {
    my ( $self, $msg ) = @_;
    my $logger_object = $self->[_logger_object_];
    if ($logger_object) {
        $logger_object->write_logfile_entry($msg);
    }
    return;
} ## end sub write_logfile_entry

sub new {
    my ( $class, $line_sink_object, $rOpts, $logger_object ) = @_;

    my $self = [];
    $self->[_line_sink_object_]            = $line_sink_object;
    $self->[_logger_object_]               = $logger_object;
    $self->[_rOpts_]                       = $rOpts;
    $self->[_output_line_number_]          = 1;
    $self->[_consecutive_blank_lines_]     = 0;
    $self->[_consecutive_nonblank_lines_]  = 0;
    $self->[_consecutive_new_blank_lines_] = 0;
    $self->[_first_line_length_error_]     = 0;
    $self->[_max_line_length_error_]       = 0;
    $self->[_last_line_length_error_]      = 0;
    $self->[_first_line_length_error_at_]  = 0;
    $self->[_max_line_length_error_at_]    = 0;
    $self->[_last_line_length_error_at_]   = 0;
    $self->[_line_length_error_count_]     = 0;
    $self->[_max_output_line_length_]      = 0;
    $self->[_max_output_line_length_at_]   = 0;
    $self->[_rK_checklist_]                = [];
    $self->[_K_arrival_order_matches_]     = 0;
    $self->[_K_sequence_error_msg_]        = EMPTY_STRING;
    $self->[_K_last_arrival_]              = -1;
    $self->[_save_logfile_]                = defined($logger_object);

    # save input stream name for local error messages
    $input_stream_name = EMPTY_STRING;
    if ($logger_object) {
        $input_stream_name = $logger_object->get_input_stream_name();
    }

    bless $self, $class;
    return $self;
} ## end sub new

sub setup_convergence_test {
    my ( $self, $rlist ) = @_;
    if ( @{$rlist} ) {

        # We are going to destroy the list, so make a copy
        # and put in reverse order so we can pop values
        my @list = @{$rlist};
        if ( $list[0] < $list[-1] ) {
            @list = reverse @list;
        }
        $self->[_rK_checklist_] = \@list;
    }
    $self->[_K_arrival_order_matches_] = 1;
    $self->[_K_sequence_error_msg_]    = EMPTY_STRING;
    $self->[_K_last_arrival_]          = -1;
    return;
} ## end sub setup_convergence_test

sub get_convergence_check {
    my ($self) = @_;
    my $rlist = $self->[_rK_checklist_];

    # converged if all K arrived and in correct order
    return $self->[_K_arrival_order_matches_] && !@{$rlist};
} ## end sub get_convergence_check

sub get_output_line_number {
    return $_[0]->[_output_line_number_];
}

sub decrement_output_line_number {
    $_[0]->[_output_line_number_]--;
    return;
}

sub get_consecutive_nonblank_lines {
    return $_[0]->[_consecutive_nonblank_lines_];
}

sub get_consecutive_blank_lines {
    return $_[0]->[_consecutive_blank_lines_];
}

sub reset_consecutive_blank_lines {
    $_[0]->[_consecutive_blank_lines_] = 0;
    return;
}

# This sub call allows termination of logfile writing for efficiency when we
# know that the logfile will not be saved.
sub set_save_logfile {
    my ( $self, $save_logfile ) = @_;
    $self->[_save_logfile_] = $save_logfile;
    return;
}

sub want_blank_line {
    my $self = shift;
    unless ( $self->[_consecutive_blank_lines_] ) {
        $self->write_blank_code_line();
    }
    return;
} ## end sub want_blank_line

sub require_blank_code_lines {

    # write out the requested number of blanks regardless of the value of -mbl
    # unless -mbl=0.  This allows extra blank lines to be written for subs and
    # packages even with the default -mbl=1
    my ( $self, $count ) = @_;
    my $need   = $count - $self->[_consecutive_blank_lines_];
    my $rOpts  = $self->[_rOpts_];
    my $forced = $rOpts->{'maximum-consecutive-blank-lines'} > 0;
    foreach ( 0 .. $need - 1 ) {
        $self->write_blank_code_line($forced);
    }
    return;
} ## end sub require_blank_code_lines

sub write_blank_code_line {
    my ( $self, $forced ) = @_;

    # Write a blank line of code, given:
    #  $forced = optional flag which, if set, forces the blank line
    #    to be written. This allows the -mbl flag to be temporarily
    #    exceeded.

    my $rOpts = $self->[_rOpts_];
    return
      if (!$forced
        && $self->[_consecutive_blank_lines_] >=
        $rOpts->{'maximum-consecutive-blank-lines'} );

    $self->[_consecutive_nonblank_lines_] = 0;

    # Balance old blanks against new (forced) blanks instead of writing them.
    # This fixes case b1073.
    if ( !$forced && $self->[_consecutive_new_blank_lines_] > 0 ) {
        $self->[_consecutive_new_blank_lines_]--;
        return;
    }

    $self->[_line_sink_object_]->write_line("\n");
    $self->[_output_line_number_]++;

    $self->[_consecutive_blank_lines_]++;
    $self->[_consecutive_new_blank_lines_]++ if ($forced);

    return;
} ## end sub write_blank_code_line

use constant MAX_PRINTED_CHARS => 80;

sub write_code_line {
    my ( $self, $str, $K ) = @_;

    # Write a line of code, given
    #  $str = the line of code
    #  $K   = an optional check integer which, if if given, must
    #       increase monotonically. This was added to catch cache
    #       sequence errors in the vertical aligner.

    $self->[_consecutive_blank_lines_]     = 0;
    $self->[_consecutive_new_blank_lines_] = 0;
    $self->[_consecutive_nonblank_lines_]++;

    $self->[_line_sink_object_]->write_line($str);
    if ( chomp $str )              { $self->[_output_line_number_]++; }
    if ( $self->[_save_logfile_] ) { $self->check_line_lengths($str) }

    #----------------------------
    # Convergence and error check
    #----------------------------
    if ( defined($K) ) {

        # Convergence check: we are checking if all defined K values arrive in
        # the order which was defined by the caller.  Quit checking if any
        # unexpected K value arrives.
        if ( $self->[_K_arrival_order_matches_] ) {
            my $Kt = pop @{ $self->[_rK_checklist_] };
            if ( !defined($Kt) || $Kt != $K ) {
                $self->[_K_arrival_order_matches_] = 0;
            }
        }

        # Check for out-of-order arrivals of index K. The K values are the
        # token indexes of the last token of code lines, and they should come
        # out in increasing order.  Otherwise something is seriously wrong.
        # Most likely a recent programming change to VerticalAligner.pm has
        # caused lines to go out in the wrong order.  This could happen if
        # either the cache or buffer that it uses are emptied in the wrong
        # order.
        if ( !$self->[_K_sequence_error_msg_] ) {
            my $K_prev = $self->[_K_last_arrival_];
            if ( $K < $K_prev ) {
                chomp $str;
                if ( length($str) > MAX_PRINTED_CHARS ) {
                    $str = substr( $str, 0, MAX_PRINTED_CHARS ) . "...";
                }

                my $msg = <<EOM;
While operating on input stream with name: '$input_stream_name'
Lines have arrived out of order in sub 'write_code_line'
as detected by token index K=$K arriving after index K=$K_prev in the following line:
$str
This is probably due to a recent programming change and needs to be fixed.
EOM

                # Always die during development, this needs to be fixed
                if (DEVEL_MODE) { Fault($msg) }

                # Otherwise warn if string is not empty (added for b1378)
                $self->warning($msg) if ( length($str) );

                # Only issue this warning once
                $self->[_K_sequence_error_msg_] = $msg;

            }
        }
        $self->[_K_last_arrival_] = $K;
    }
    return;
} ## end sub write_code_line

sub write_line {
    my ( $self, $str ) = @_;

    # Write a line directly to the output, without any counting of blank or
    # non-blank lines.

    $self->[_line_sink_object_]->write_line($str);
    if ( chomp $str )              { $self->[_output_line_number_]++; }
    if ( $self->[_save_logfile_] ) { $self->check_line_lengths($str) }

    return;
} ## end sub write_line

sub check_line_lengths {
    my ( $self, $str ) = @_;

    # collect info on line lengths for logfile

    # This calculation of excess line length ignores any internal tabs
    my $rOpts   = $self->[_rOpts_];
    my $len_str = length($str);
    my $exceed  = $len_str - $rOpts->{'maximum-line-length'};
    if ( $str && substr( $str, 0, 1 ) eq "\t" && $str =~ /^\t+/g ) {
        $exceed += pos($str) * $rOpts->{'indent-columns'};
    }

    # Note that we just incremented output line number to future value
    # so we must subtract 1 for current line number
    if ( $len_str > $self->[_max_output_line_length_] ) {
        $self->[_max_output_line_length_] = $len_str;
        $self->[_max_output_line_length_at_] =
          $self->[_output_line_number_] - 1;
    }

    if ( $exceed > 0 ) {
        my $output_line_number = $self->[_output_line_number_];
        $self->[_last_line_length_error_]    = $exceed;
        $self->[_last_line_length_error_at_] = $output_line_number - 1;
        if ( $self->[_line_length_error_count_] == 0 ) {
            $self->[_first_line_length_error_]    = $exceed;
            $self->[_first_line_length_error_at_] = $output_line_number - 1;
        }

        if ( $self->[_last_line_length_error_] >
            $self->[_max_line_length_error_] )
        {
            $self->[_max_line_length_error_]    = $exceed;
            $self->[_max_line_length_error_at_] = $output_line_number - 1;
        }

        if ( $self->[_line_length_error_count_] < MAX_NAG_MESSAGES ) {
            $self->write_logfile_entry(
                "Line length exceeded by $exceed characters\n");
        }
        $self->[_line_length_error_count_]++;
    }
    return;
} ## end sub check_line_lengths

sub report_line_length_errors {
    my $self = shift;

    # Write summary info about line lengths to the log file

    my $rOpts                   = $self->[_rOpts_];
    my $line_length_error_count = $self->[_line_length_error_count_];
    if ( $line_length_error_count == 0 ) {
        $self->write_logfile_entry(
            "No lines exceeded $rOpts->{'maximum-line-length'} characters\n");
        my $max_output_line_length    = $self->[_max_output_line_length_];
        my $max_output_line_length_at = $self->[_max_output_line_length_at_];
        $self->write_logfile_entry(
"  Maximum output line length was $max_output_line_length at line $max_output_line_length_at\n"
        );

    }
    else {

        my $word = ( $line_length_error_count > 1 ) ? "s" : EMPTY_STRING;
        $self->write_logfile_entry(
"$line_length_error_count output line$word exceeded $rOpts->{'maximum-line-length'} characters:\n"
        );

        $word = ( $line_length_error_count > 1 ) ? "First" : EMPTY_STRING;
        my $first_line_length_error    = $self->[_first_line_length_error_];
        my $first_line_length_error_at = $self->[_first_line_length_error_at_];
        $self->write_logfile_entry(
" $word at line $first_line_length_error_at by $first_line_length_error characters\n"
        );

        if ( $line_length_error_count > 1 ) {
            my $max_line_length_error    = $self->[_max_line_length_error_];
            my $max_line_length_error_at = $self->[_max_line_length_error_at_];
            my $last_line_length_error   = $self->[_last_line_length_error_];
            my $last_line_length_error_at =
              $self->[_last_line_length_error_at_];
            $self->write_logfile_entry(
" Maximum at line $max_line_length_error_at by $max_line_length_error characters\n"
            );
            $self->write_logfile_entry(
" Last at line $last_line_length_error_at by $last_line_length_error characters\n"
            );
        }
    }
    return;
} ## end sub report_line_length_errors
1;
