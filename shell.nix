let
  pkgs = import ./nixpkgs.nix { };
in
with pkgs; pkgs.mkShell rec {
  buildInputs = [
    scala
    sbt
    openssl
    openjdk
  ];
  shellHook = ''
    export JAVA_HOME="${openjdk}/lib/openjdk";
  '';  
}
