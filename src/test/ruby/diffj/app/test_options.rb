#!/usr/bin/jruby -w
# -*- ruby -*-

require 'test/unit'
require 'java'
require 'rubygems'
require 'riel'
require 'diffj/app/options'

include Java

Log::level = Log::DEBUG
Log.set_widths(-15, 5, -50)

class DiffJOptionsTest < Test::Unit::TestCase
  include Loggable
  
  def assert_option allexpvals, key, optval
    assert_equal allexpvals[key], optval, key.to_s.bold
  end

  def assert_options expvals, opts
    allexpvals = default_option_values.merge expvals
    assert_option allexpvals, :brief, opts.show_brief_output
    assert_option allexpvals, :context, opts.show_context_output
    assert_option allexpvals, :highlight, opts.highlight_output
    assert_option allexpvals, :version, opts.show_version
    assert_option allexpvals, :from, opts.from_source
    assert_option allexpvals, :to, opts.to_source
    assert_option allexpvals, :recurse, opts.recurse
    assert_option allexpvals, :first, opts.first_file_name
    assert_option allexpvals, :second, opts.second_file_name
    assert_option allexpvals, :help, opts.show_help
    assert_option allexpvals, :verbose, opts.verbose
  end

  def default_option_values
    values = Hash.new
    values[:brief] = false
    values[:context] = false
    values[:highlight] = false
    values[:version] = false
    values[:from] = "1.5"
    values[:to] = "1.5"
    values[:recurse] = false
    values[:first] = nil
    values[:second] = nil
    values[:help] = false
    values[:verbose] = false
    values
  end

  def run_test args, exp
    # opts = org.incava.diffj.Options.new
    opts = DiffJ::Options.new
    names = opts.process args
    info "opts: #{opts}".bold.green
    info "args: #{args}".bold.green
    assert_options exp, opts
  end

  def test_version
    run_test %w{ --version }, { :version => true }
    run_test %w{ -v }, { :version => true }
  end

  def test_brief
    run_test %w{ --brief }, { :brief => true }
  end

  def test_context
    # context sets highlight on and brief off
    run_test %w{ --context }, { :context => true, :brief => false, :highlight => true }
    run_test %w{ --brief --context }, { :context => true, :brief => false, :highlight => true }
    # order matters with the optparse implementation:
    # run_test %w{ --context --brief }, { :context => true, :brief => false, :highlight => true }
  end

  def test_highlight
    # highlight turns off brief
    run_test %w{ --highlight }, { :highlight => true, :brief => false }
    run_test %w{ --brief --highlight }, { :highlight => true, :brief => false }
    # order matters with the optparse implementation:
    # run_test %w{ --highlight --brief }, { :highlight => true, :brief => false }
  end

  def test_recurse
    run_test %w{ --recurse }, { :recurse => true }
    run_test %w{ -r }, { :recurse => true }
  end

  def test_from_source
    run_test %w{ }, { :from => "1.5" }
    run_test %w{ --from-source 1.4 }, { :from => "1.4" }
  end

  def test_to_source
    run_test %w{ }, { :to => "1.5" }
    run_test %w{ --to-source 1.4 }, { :to => "1.4" }
  end

  def test_source
    run_test %w{ }, { :from => "1.5" }
    run_test %w{ }, { :to => "1.5" }
    run_test %w{ --source 1.4 }, { :from => "1.4", :to => "1.4" }
  end

  def test_unified_format
    # this is for svn diff --diff-cmd cmd, which passes "-u, -L first, -L second, file1, file2":

    # ignored for now:
    run_test %w{ -u }, { }
  end

  def test_from_and_to_names
    # this is for svn diff --diff-cmd cmd, which passes "-u, -L first, -L second, file1, file2":

    %w{ -L --name }.each do |nametag|
      run_test [ nametag, "Abc.java" ], { :first => "Abc.java", :second => nil }
      run_test [ '-u', nametag, "Abc.java" ], { :first => "Abc.java", :second => nil }

      %w{ -L --name }.each do |secondtag|
        run_test [ nametag, "Abc.java", secondtag, "Xyz.java" ], { :first => "Abc.java", :second => "Xyz.java" }
        run_test [ '-u', nametag, "Abc.java", secondtag, "Xyz.java" ], { :first => "Abc.java", :second => "Xyz.java" }
      end
    end
  end

  def test_help
    run_test %w{ -h --help }, { :help => true }
  end

  def test_verbose
    run_test %w{ --verbose }, { :verbose => true }
  end

  def test_names
    run_test %w{ --verbose abc xyz }, { :verbose => true }
  end
end