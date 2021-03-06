#!/usr/bin/jruby -w
# -*- ruby -*-

require 'diffj/tc'
require 'diffj/fdiff/fdiff'
require 'diffj/fdiff/writers/writer'
require 'diffj/fdiff/writers/no_context'

include Java

class DiffJ::WriterTestCase < DiffJ::TestCase
  include Loggable

  FROMCONT  = Array.new
  #           0         1         2         3         4         5
  #            12345678901234567890123456789012345678901234567890
  FROMCONT << "And pleasant was his absolution."                   # 1
  FROMCONT << "He was an easy man to give penance,"  # delete      # 2
  FROMCONT << "There as he wist to have a good pittance:" # change # 3
  FROMCONT << "For unto a poor order for to give"                  # 4
  FROMCONT << "Is signe that a man is well y-shrive."              # 5
  FROMCONT << "For if he gave, he durste make avant," # change     # 6
  FROMCONT << "He wiste that the man was repentant."  # change     # 7

  TOCONT    = Array.new
  #           0         1         2         3         4         5
  #            12345678901234567890123456789012345678901234567890
  TOCONT   << "And pleasant was his absolution."                   # 1
  # TOCONT   << "He was an easy man to give penance,"           
  TOCONT   << "Where he know he would get good payment"            # 2
  TOCONT   << "For unto a poor order for to give"                  # 3
  TOCONT   << "Is signe that a man is well y-shrive."              # 4
  TOCONT   << "For if he gave, he dared to boast,"       # change  # 5
  TOCONT   << "He knew that the man was repentant."      # change  # 6
  TOCONT   << "For many a man so hard is of his heart,"  # add     # 7
  TOCONT   << "He may not weep although him sore smart." # add     # 8

  def create_exp_str lines, ch
    lines.collect { |line| "#{ch} #{line}\n" }.join("")
  end

  def add_lines str, lines, range, ch = " "
    range && range.each do |idx|
      str << "#{ch} #{lines[idx]}\n"
    end
    str
  end

  def run_delta_test expected, fdiff, &blk
    dw = get_writer FROMCONT, TOCONT
    str = ""    
    blk.call dw, str, fdiff
    assert_equal expected, str
  end

  def run_change_test expected, &blk
    fdc = DiffJ::FDiffChange.new "text changed", :locranges => [ locrg(6, 20, 6, 36), locrg(5, 20, 5, 33) ]
    run_delta_test expected, fdc, &blk
  end

  def run_add_test expected, &blk
    fda = DiffJ::FDiffAdd.new "text added", :locranges => [ locrg(6, 1, 6, 1), locrg(7, 1, 8, 40) ]
    run_delta_test expected, fda, &blk
  end

  def run_delete_test expected, &blk
    fda = DiffJ::FDiffDelete.new "text deleted", :locranges => [ locrg(2, 1, 2, 35), locrg(2, 1, 2, 1) ]
    run_delta_test expected, fda, &blk
  end

  def get_writer fromcont, tocont
    get_writer_class.new fromcont, tocont
  end

  def get_writer_class
  end

  def default_test
    info "self: #{self}"
  end
end
