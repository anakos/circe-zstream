{
  cfg ? { }
}:
let
  sources    = import ./nix/sources.nix;
  pkgs       = import sources.nixpkgs cfg;
in
  pkgs
