package model;

class CacheKey<T>{
    final T key;
    Long timestamp;

    CacheKey( T key ){
        this.key = key;
        this.timestamp = System.currentTimeMillis();
    }

    void resetTimeStamp(){
        timestamp = System.currentTimeMillis();
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 43 * hash + ( this.key != null ? this.key.hashCode() : 0 );
        return hash;
    }

    @Override
    public boolean equals( Object obj ){
        if( obj == null ){
            return false;
        }
        if( !( obj instanceof CacheKey ) ){
            return false;
        }
        CacheKey cacheKey = ( CacheKey ) obj;
        return this.key.equals( cacheKey.key );
    }

    @Override
    public String toString(){
        return String.format( "key[%s]" , key.toString() );
    }

}
