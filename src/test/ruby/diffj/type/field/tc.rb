#!/usr/bin/jruby -w
# -*- ruby -*-

require 'diffj/type/tc'

include Java

module DiffJ::Type::Field
  class TestCase < DiffJ::Type::TestCase
  end
end
