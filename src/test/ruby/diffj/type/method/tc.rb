#!/usr/bin/jruby -w
# -*- ruby -*-

require 'diffj/type/tc'

include Java

module DiffJ::Type::Method
  class TestCase < DiffJ::Type::TestCase
  end
end
