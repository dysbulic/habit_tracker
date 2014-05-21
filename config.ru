require 'rack/reverse_proxy'

use Rack::ReverseProxy do
  reverse_proxy /^\/db\/?(.*)$/, 'https://doh.cloudant.com/$1'
end

use Rack::Static,
  :urls => ['/images', '/js', '/css', '/fonts', '/tests'],
  :root => 'www'

run lambda { |env|
  [
    200,
    {
      'Content-Type'  => 'text/html',
      'Cache-Control' => 'public, max-age=86400'
    },
    File.open('www/index.html', File::RDONLY)
  ]
}
