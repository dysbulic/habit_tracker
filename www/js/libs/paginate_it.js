// From: https://github.com/efigarolam/ember-blog
(function() {
    App.PaginateIt = Ember.Mixin.create({
        maxPerPage: 20,
        setup: function(resource) {
            this.set('currentPage', this.get('content.content.firstObject'));
            return this.set(resource, this.get("currentPage." + resource));
        },
        perPage: (function() {
            return this.get('pagination.perPage');
        }).property('pagination'),
        hasPrevious: (function() {
            return this.get('pageNumber') > 1;
        }).property('pageNumber'),
        hasNext: (function() {
            return this.get('pageNumber') < this.get('pagination.totalPages');
        }).property('pageNumber'),
        pagination: (function() {
            this.setup(this.get('resource'));
            return this.get('content.content.firstObject');
        }).property('content'),
        pageNumber: (function() {
            return parseInt(this.get('pagination.page'));
        }).property('pagination'),
        pages: (function() {
            var pages, self, _i, _ref, _ref1, _results;
            self = this;
            pages = Em.A();
            (function() {
                _results = [];
                for (var _i = _ref = this.get('firstPage'), _ref1 = this.get('lastPage'); _ref <= _ref1 ? _i <= _ref1 : _i >= _ref1; _ref <= _ref1 ? _i++ : _i--){ _results.push(_i); }
                return _results;
            }).apply(this).forEach(function(page) {
                return pages.pushObject({
                    pageNumber: page,
                    active: page === self.get('pageNumber')
                });
            });
            return pages;
        }).property('@each'),
        firstPage: (function() {
            var firstPage;
            firstPage = 1;
            if (this.get('pageNumber') - 5 > 0) {
                firstPage = this.get('pageNumber') - 5;
            }
            return firstPage;
        }).property('pageNumber'),
        lastPage: (function() {
            var lastPage;
            lastPage = this.get('pagination.totalPages');
            if (this.get('firstPage') + 5 < this.get('pagination.totalPages')) {
                lastPage = this.get('firstPage') + 5;
            }
            return lastPage;
        }).property('pagination.totalPages'),
        changePage: function(pageNumber) {
            var params, self;
            params = {
                page: pageNumber,
                per_page: this.get('perPage')
            };
            self = this;
            return this.store.find('readingSearch', params).then(function(readingSearch) {
                return self.set('content', readingSearch);
            });
        },
        switchPerPage: function(perPage) {
            this.set('maxPerPage', this.get('perPage'));
            return this.set('perPage', perPage);
        },
        actions: {
            goPage: function(page) {
                return this.changePage(page);
            },
            goNext: function() {
                if (this.get('hasNext')) {
                    return this.changePage(this.get('pageNumber') + 1);
                }
            },
            goPrev: function() {
                if (this.get('hasPrevious')) {
                    return this.changePage(this.get('pageNumber') - 1);
                }
            },
            goFirst: function() {
                if (this.get('hasPrevious')) {
                    return this.changePage(1);
                }
            },
            goLast: function() {
                if (this.get('hasNext')) {
                    return this.changePage(this.get('pagination.totalPages'));
                }
            },
            changePerPage: function(newPerPage) {
                this.switchPerPage(newPerPage);
                return this.changePage(1);
            }
        }
    });

}).call(this);
