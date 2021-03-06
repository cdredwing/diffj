#!/usr/bin/jruby -w
# -*- ruby -*-

require 'test/unit'
require 'java'
require 'rubygems'
require 'riel'
require 'diffj'

include Java

class DiffJTest < Test::Unit::TestCase
  include Loggable
  
  TESTBED_DIR = '/proj/org/incava/diffj/src/test/resources'
  
  def run_test dirname
    fnames = %w{ d0 d1 }.collect { |subdir| TESTBED_DIR + '/' + dirname + '/' + subdir }
    brief = false
    context = true
    highlight = true
    recurse = true
    fromname = nil
    fromver = "1.5"
    toname = nil
    tover = "1.5"
    
    diffj = DiffJ::CLI.new brief, context, highlight, recurse, fromname, fromver, toname, tover
    diffj.process_names fnames
    assert_not_nil diffj
  end

  def test_nothing
  end

  # def test_diffj_pkgs
  #   run_test 'pkgdiffs'
  # end

  # def test_diffj_imports
  #   run_test 'impdiffs'
  # end

  # def test_diffj_types
  #   run_test 'typesdiffs'
  # end

  # def test_diffj_type
  #   run_test 'typediffs'
  # end
end
