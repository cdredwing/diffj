#!/usr/bin/jruby -w
# -*- ruby -*-

require 'test/unit'
require 'java'
require 'rubygems'
require 'riel'
require 'diffj'
require 'diffj/io/writer'

include Java

java_import org.incava.analysis.DetailedReport

class DiffJ::WriterContextHighlightTestCase < TestCase
  def test_all
    # not yet implemented
  end
end